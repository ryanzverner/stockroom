Sequel.migration do
  up do
    create_table(:apprenticeships) do
      primary_key :id

      foreign_key :person_id, :people, :foreign_key_constraint_name => 'apprenticeship_fkey_person_id'

      String :skill_level , :size => 200

      DateTime :start
      DateTime :end

      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:apprenticeships)
  end
end
