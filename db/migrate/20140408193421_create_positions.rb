Sequel.migration do
  up do
    create_table(:positions) do
      primary_key :id

      String :name, :size => 200

      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:positions)
  end
end
